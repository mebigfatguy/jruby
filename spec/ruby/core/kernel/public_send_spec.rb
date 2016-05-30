require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)
require File.expand_path('../../../shared/basicobject/send', __FILE__)

describe "Kernel#public_send" do
  it "invokes the named public method" do
    class KernelSpecs::Foo
      def bar
        'done'
      end
    end
    KernelSpecs::Foo.new.public_send(:bar).should == 'done'
  end

  it "invokes the named alias of a public method" do
    class KernelSpecs::Foo
      def bar
        'done'
      end
      alias :aka :bar
    end
    KernelSpecs::Foo.new.public_send(:aka).should == 'done'
  end

  it "raises a NoMethodError if the method is protected" do
    class KernelSpecs::Foo
      protected
      def bar
        'done'
      end
    end
    lambda { KernelSpecs::Foo.new.public_send(:bar)}.should raise_error(NoMethodError)
  end

  it "raises a NoMethodError if the named method is private" do
    class KernelSpecs::Foo
      private
      def bar
        'done2'
      end
    end
    lambda {
      KernelSpecs::Foo.new.public_send(:bar)
    }.should raise_error(NoMethodError)
  end

  it "raises a NoMethodError if the named method is an alias of a private method" do
    class KernelSpecs::Foo
      private
      def bar
        'done2'
      end
      alias :aka :bar
    end
    lambda {
      KernelSpecs::Foo.new.public_send(:aka)
    }.should raise_error(NoMethodError)
  end

  it "raises a NoMethodError if the named method is an alias of a protected method" do
    class KernelSpecs::Foo
      protected
      def bar
        'done2'
      end
      alias :aka :bar
    end
    lambda {
      KernelSpecs::Foo.new.public_send(:aka)
    }.should raise_error(NoMethodError)
  end

  it_behaves_like(:basicobject_send, :public_send)
end
